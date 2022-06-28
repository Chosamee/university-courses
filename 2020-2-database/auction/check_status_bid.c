#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <mysql/mysql.h>
#include "structure.h"


int how_many_bidders(MYSQL *conn, int primary_num){
    MYSQL_RES *sql_result;
    MYSQL_ROW sql_row;
    int bidder_num;
    char query[100];
    int item_cnt = 1;

    sprintf(query, "select count(*) from bid_history where num=%d", primary_num);
    mysql_query(conn, query);
    sql_result=mysql_store_result(conn);
    while((sql_row=mysql_fetch_row(sql_result))){
        bidder_num = atoi(sql_row[0]);
    }
    mysql_free_result(sql_result);
    return bidder_num;
}


void check_status_bid(MYSQL* conn, user* user1){
    char query[250];
    MYSQL_RES *sql_result;
    MYSQL_ROW sql_row;
    int item_cnt = 1;
    sprintf(query, "select num, description, is_traded, bid_price, buyer, posted_date, bid_end_date, bidder_num, sold_date, buy_now_price\
                     from item_list join bid_hitory where bidder=\'%s\'", user1->email);

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
            printf("status: %s bids\n", sql_row[7]);
            printf("current bidding price: %s\n", sql_row[3]);
            printf("current highest bidder: %s\n", sql_row[4]);
            printf("date posted: %s\n", sql_row[5]);
            printf("bid ending date: %s\n\n", sql_row[6]);
        }
        item_cnt++;
//         for(int j=0;j<field;j++)
//                 printf("%12s",sql_row[j]);
//                 printf("\n");
    }

    mysql_free_result(sql_result);
}