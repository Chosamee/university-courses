#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <mysql/mysql.h>
#include <time.h>
#include "structure.h"

// 메모.
// 모든 item에 대해
// item_num 올린시간, 마감시간, 입찰횟수, seller, 판매완료 여부?, 최고입찰자, buyer, 기타등등


void upload_item(MYSQL *conn, user* user2, int cat_num, int cond_num, char* descript, int price_init, char* end_date){
    char* category[] = {"", "Electronics", "Books", "Home", "Clothing", "Sporting Goods"};
    char* condition[] = {"", "New", "Like-New", "Used (Good)", "Used (Acceptable)"};

    time_t t = time(NULL);
    struct tm tm = *localtime(&t);
    char posted_date[20];
    sprintf(posted_date, "%d-%d-%d %d:%d", tm.tm_year+1900, tm.tm_mon+1, tm.tm_mday, tm.tm_hour, tm.tm_min);

    char query[200];
    sprintf(query, "insert into item_list(seller, category, cond, description, buy_now_price, posted_date, bid_end_date) values(\'%s\', \'%s\', \'%s\', \'%s\', %d,\'%s\', \'%s\')",
                    user2 -> email,  category[cat_num], condition[cond_num], descript, price_init, posted_date, end_date);
    mysql_query(conn, query);
}


void sell_item(MYSQL *conn, user* user2){
    int cat_num, cond_num;
    int price_init = -1;
    char descript[200];
    char e_date[10];
    char e_time[10];
    char end_date[20];

    printf("----< Sell item >\n"
            "---- select from the following category : (Enter the number)\n"
            "(1) Electronics\n"
            "(2) Books\n"
            "(3) Home\n"
            "(4) Clothing\n"
            "(5) Sporting Goods\n");
    printf("Your Choice: ");
    scanf("%d", &cat_num);
    getchar();

    printf( "---- condition: (Enter the number)\n"
            "(1) New\n"
            "(2) Like-New\n"
            "(3) Used (Good)\n"
            "(4) Used (Acceptable)\n");
    printf("Your Choice: ");
    scanf("%d", &cond_num);
    getchar();

    printf("---- description: ");
    scanf("%s", descript);
    
    while(price_init==-1){
        while (getchar() != '\n');
        printf("---- buy-it-now price: ");
        scanf("%d", &price_init);
    }

    printf("---- bid ending date (yyyy-mm-dd HH:mm, e.g. 2020-12-04 23:59) : ");
    scanf("%s", e_date);
    scanf("%s", e_time);
    sprintf(end_date, "%s %s", e_date, e_time);
    upload_item(conn, user2, cat_num, cond_num,descript, price_init, end_date);
}