#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <mysql/mysql.h>
#include "structure.h"

void status_my_item(MYSQL *conn, user *user1){
    MYSQL_RES *sql_result;
    MYSQL_ROW sql_row;
    int query_stat;
    int item_cnt = 1;

    printf("----< Status of Your Item Listed on Auction >\n");

    char query[200];
    sprintf(query, "select num, description, is_traded, bid_price, buyer, posted_date, bid_end_date, bidder_num, sold_date, buy_now_price from item_list where seller=\'%s\'", user1->email);

    mysql_query(conn, query);
    sql_result=mysql_store_result(conn);

    int field=mysql_num_fields(sql_result);

    while((sql_row=mysql_fetch_row(sql_result))){
        printf("[Item %d]\n", item_cnt);
        printf("description: %s\n", sql_row[1]);

        if(atoi(sql_row[2])){
            printf("status: sold\n");
            printf("buyer: %s\n", sql_row[4]);
            printf("sold date: %s\n\n", sql_row[8]);
        }
        else{
            printf("status: %d bids\n", how_many_bidders(conn, atoi(sql_row[0])));
            printf("current bidding price: %s\n", sql_row[3]);
            printf("current highest bidder: %s\n", sql_row[4]);
            printf("date posted: %s\n", sql_row[5]);
            printf("bid ending date: %s\n\n", sql_row[6]);
        }
        item_cnt++;
    }

    mysql_free_result(sql_result);
}