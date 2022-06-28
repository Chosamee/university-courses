#include <mysql/mysql.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "login_menu.c"


int main(void){
    MYSQL *conn = mysql_init(NULL);
    MYSQL_RES *sql_result;
    MYSQL_ROW sql_row;
    int query_stat;
    
    int num;
    user* now_user;
    
    if (mysql_real_connect(conn, "localhost", "db17313260","changethis",
                                    "db17313260", 3306, (char *)NULL, 0) == NULL){
        fprintf(stderr, "Mysql connection error : %s", mysql_error(conn));
        return 1;
    }

    login_menu(conn);


    // char *buff2 = "select * from student";
    // mysql_query(conn,buff2);
    // sql_result=mysql_store_result(conn);
    // int field=mysql_num_fields(sql_result);

    // while((sql_row=mysql_fetch_row(sql_result))){
    //             for(int j=0;j<field;j++)
    //                     printf("%12s",sql_row[j]);
    //                     printf("\n");
    //     }




    free(now_user);
    
    // query_stat = mysql_query(connection, "select * from student");
    // if (query_stat != 0){
    //     fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
    //     return 1;
    // }

    // sql_result = mysql_store_result(connection);

    // printf("이름 주소 전화번호");
    // while ( (sql_row = mysql_fetch_row(sql_result)) != NULL )
    // {
    //     printf("%s %s %s", sql_row[0], sql_row[1], sql_row[2]);
    // }

    /*
    mysql_free_result(sql_result);

    mysql_close(connection); 
    */
}
