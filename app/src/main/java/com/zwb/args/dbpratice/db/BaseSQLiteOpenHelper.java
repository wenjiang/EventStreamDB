package com.zwb.args.dbpratice.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zwb.args.dbpratice.annotation.Column;
import com.zwb.args.dbpratice.annotation.Table;
import com.zwb.args.dbpratice.cache.DatabaseCache;
import com.zwb.args.dbpratice.exception.NoSuchTableException;
import com.zwb.args.dbpratice.model.BaseTable;
import com.zwb.args.dbpratice.util.LogUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 基本的SQLiteOpenHelper操作
 * Created by wenbiao_zheng on 2015/3/10.
 */
public class BaseSQLiteOpenHelper extends SQLiteOpenHelper {
    private Context context;

    public BaseSQLiteOpenHelper(Context context, String name, int version) {
        super(context, name, null, version);

        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    /**
     * 销毁指定的表
     *
     * @param db    数据库
     * @param table 表名
     */
    private void dropTable(SQLiteDatabase db, String table) {
        String deleteSql = "drop table if exists " + table;
        db.execSQL(deleteSql);
    }

    /**
     * 创建指定的表
     *
     * @param db    数据库
     * @param table 表名
     */
    private void createTable(SQLiteDatabase db, String table) {
        try {
            StringBuilder sql = new StringBuilder("create table if not exists ");
            BaseTable entity = (BaseTable) (Class.forName(table).newInstance());
            String tableName = getTableName(entity);
            sql.append(tableName);
            sql.append(" (id integer primary key autoincrement, ");
            List<String> columnList = getColumns(entity);
            for (int i = 0, length = columnList.size(); i < length; i++) {
                sql.append(columnList.get(i) + " ");
                if (i == length - 1) {
                    break;
                }
                sql.append(", ");
            }
            sql.append(");");
            db.execSQL(sql.toString());
        } catch (InstantiationException e) {
            LogUtil.e(e.toString());
        } catch (IllegalAccessException e) {
            LogUtil.e(e.toString());
        } catch (ClassNotFoundException e) {
            LogUtil.e(e.toString());
        } catch (NoSuchTableException e) {
            LogUtil.e(e.toString());
        }
    }

    /**
     * 从表中获取列名
     *
     * @param table 表名
     * @return
     */
    private List<String> getColumnFrom(String table) {
        List<String> columnList = new ArrayList<>();
        try {
            BaseTable entity = (BaseTable) (Class.forName(table).newInstance());
            columnList = getColumns(entity);
        } catch (InstantiationException e) {
            LogUtil.e(e.toString());
        } catch (IllegalAccessException e) {
            LogUtil.e(e.toString());
        } catch (ClassNotFoundException e) {
            LogUtil.e(e.toString());
        }

        return columnList;
    }

    /**
     * 创建表
     *
     * @param db SQLiteDatabase
     */
    private void createTable(SQLiteDatabase db) {
        SharedPreferencesManager.init(context);
        List<String> oldTableList = SharedPreferencesManager.getInstance().getListString("tables");
        DatabaseCache cache = DatabaseCache.getInstance();
        Set<String> tableSet = cache.getTableName();
        if (oldTableList.size() != 0) {

            for (String table : oldTableList) {
                if (!tableSet.contains(table)) {
                    dropTable(db, table);
                }
            }

            for (String table : tableSet) {
                if (!oldTableList.contains(table)) {
                    createTable(db, table);
                }
            }
        } else {
            for (String table : tableSet) {
                createTable(db, table);
            }
        }
        List<String> tableList = new ArrayList<>();
        for (String table : tableSet) {
            BaseTable newEntity = null;
            try {
                newEntity = (BaseTable) (Class.forName(table).newInstance());
                String tableName = getTableName(newEntity);
                tableList.add(tableName);
            } catch (InstantiationException e) {
                LogUtil.e(e.toString());
            } catch (IllegalAccessException e) {
                LogUtil.e(e.toString());
            } catch (ClassNotFoundException e) {
                LogUtil.e(e.toString());
            } catch (NoSuchTableException e) {
                LogUtil.e(e.toString());
            }
        }

        SharedPreferencesManager.getInstance().putListString("tables", tableList);

        int version = SharedPreferencesManager.getInstance().getInt("version");
        if (version == 0 || cache.getVersion() <= version) {
            return;
        }
        for (String tableEntity : tableSet) {
            try {
                BaseTable entity = (BaseTable) (Class.forName(tableEntity).newInstance());
                db.beginTransaction();
                String table = getTableName(entity);
                String selectSql = "select * from " + table;
                Cursor cursor = db.rawQuery(selectSql, null);
                List<String> oldColumns = new ArrayList<>();
                for (String column : cursor.getColumnNames()) {
                    oldColumns.add(column);
                }
                String alterSql = "alter table " + table + " rename to " + table + "_temp";
                db.execSQL(alterSql);

                createTable(db, tableEntity);

                List<String> newColumns = getColumnFrom(tableEntity);
                StringBuilder upgradeSqlBuilder = new StringBuilder("insert into " + table + " select id, ");
                int i = 0;
                for (String column : newColumns) {
                    if (oldColumns.contains(column)) {
                        upgradeSqlBuilder.append(column + ", ");
                        i++;
                    }
                }

                if ((i != 0) && (i < newColumns.size())) {
                    for (int j = 0, length = newColumns.size() - i; j < length; j++) {
                        upgradeSqlBuilder.append("'', ");
                    }
                }

                String upgradeStr = upgradeSqlBuilder.toString();
                String upgradeSql = upgradeStr.substring(0, upgradeStr.length() - 2) + " from " + table + "_temp";
                db.execSQL(upgradeSql);
                dropTable(db, table + "_temp");
                db.setTransactionSuccessful();
                db.endTransaction();
            } catch (InstantiationException e) {
                LogUtil.e(e.toString());
            } catch (IllegalAccessException e) {
                LogUtil.e(e.toString());
            } catch (ClassNotFoundException e) {
                LogUtil.e(e.toString());
            } catch (NoSuchTableException e) {
                LogUtil.e(e.toString());
            }
        }

        getTableList(db);
    }

    /**
     * 获取表名
     *
     * @param entity 表对象
     * @return 表名
     * @throws NoSuchTableException
     */
    private String getTableName(BaseTable entity) throws NoSuchTableException {
        String tableName = "";
        if (entity.getClass().isAnnotationPresent(Table.class)) {
            Table table = entity.getClass().getAnnotation(Table.class);
            tableName = table.table();

            if (tableName.length() == 0) {
                throw new NoSuchTableException("The table + " + entity.getClass().getSimpleName() + " is not exist");
            }
        }

        return tableName;
    }

    /**
     * 获取列名
     *
     * @param entity 表对象
     * @return 列名的List
     */
    private List<String> getColumns(BaseTable entity) {
        Set<String> columnSet = new HashSet<>();
        List<String> columnList = new ArrayList<>();
        java.lang.reflect.Field[] fields = entity.getClass().getDeclaredFields();
        for (java.lang.reflect.Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                Column meta = field.getAnnotation(Column.class);
                String column = meta.column();
                if (column.equals("")) {
                    column = field.getName();
                }

                if (!columnSet.contains(column)) {
                    columnSet.add(column);
                    columnList.add(column);
                }
            }
        }

        return columnList;
    }

    /**
     * 打印创建好的表的名字
     *
     * @param db 数据库
     */
    private void getTableList(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("select name from sqlite_master where type='table' order by name", null);
        while (cursor.moveToNext()) {
            //遍历出表名
            String name = cursor.getString(0);
            LogUtil.e(name);
        }
    }
}
