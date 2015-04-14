# EventStreamDB
Android上的基于事件流的数据库

         采用事件流的思想设计应用的数据操作，简单点讲，就是对数据的任何变更操作都处理成事件，然后将相应的数据存储在缓存中，在UI需要的时候，
     再从缓存中读取数据。
     
          这样的好处就是任何对数据的变更操作都不会马上写入数据库，而是在一定的时机才一次性写入或者一次性读取，这样就能避免数据库的读写竞争
     和频繁打开数据库的无谓损耗。
         
基本的使用方法

1.创建数据库和表
         
          在assets文件夹下创建database.xml文件，里面配置数据库的名字，版本号和数据库的表：
          
          <?xml version="1.0" encoding="utf-8"?>
          <database>
              <!-- 数据库名称 -->
              <dbname value="zwb.db"></dbname>
          
              <!-- 数据库版本 -->
              <version value="1"></version>
          
              <!-- 数据库表 -->
              <list>
                  <mapping class="com.zwb.args.dbpratice.model.Status"></mapping>
                  <mapping class="com.zwb.args.dbpratice.model.User"></mapping>
              </list>
          </database>
          
          然后初始化DatabaseCache：
                 DatabaseCache cache = DatabaseCache.getInstance(this);
          该操作应该是在Application中声明，因为该动作涉及到数据库和表的创建。
                  
2.基本使用

           声明一个model类，继承自BaseTable:
                 @Table(table = "status")
                 public class Status extends BaseTable {
                     @Column
                     private String name;
                     @Column
                     private String statusId;
                 
                     public void setName(String name) {
                         this.name = name;
                     }
                 
                     public String getName() {
                         return name;
                     }
                 
                     public void setStatusId(String id) {
                         this.statusId = id;
                     }
                 
                     public String getStatusId() {
                         return statusId;
                     }
                 }
           
           其中，@Table声明的是该model对应的表的名字，@Column声明的是该字段对应的数据库中的类型。
           如果该字段的类型和数据库中的类型不一致，可以通过@ColumnType来指定类型。
         
（1）数据插入
            
              Status status = new Status();
              status.setName("转发");
              status.setStatusId("01");
              InsertEvent insertStatusEvent = new InsertEvent();
              insertStatusEvent.to(Status.class).insert(status);
              
（2）数据更新

             UpdateEvent updateEvent = new UpdateEvent();
             updateEvent.to(Status.class).where("id", "01").update("name", "你好");
             
（3）数据查询

              List<Status> statusList = cache.from(Status.class).where("statusId", "01").find();
              
              这样就是查询Status表中的statusId为01的所有记录。
              
              当然，也可以查询所有数据：
              
              List<Status> statusList = cache.from(Status.class).findAll();

（4）数据读取

             DatabaseCache cache = DatabaseCache.getInstance(this);
             List<Status> statusList =  cache.readFromDb(Status.class);
             
             该操作应该在Application中执行，然后执行相应的数据插入：
             for(Status status : statusList){
                    InsertEvent insertEvent = new InsertEvent();
                    insertEvent.to(Status.class).insert(status);
             }
             
             这样数据就会从数据库转移到事件流中。
          
（5）数据存储

            DatabaseCache cache = DatabaseCache.getInstance(this);
            cache.insertToDb(Status.class);
            
            这样就会将和Status有关的数据插入到数据库中。

