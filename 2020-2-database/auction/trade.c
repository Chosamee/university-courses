#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <mysql/mysql.h>
#include "structure.h"


int highest_bid(MYSQL *conn, int primary_num){
    MYSQL_RES *sql_result;
    MYSQL_ROW sql_row;
    char query[100];
    int highest_bid;
    sprintf(query, "select bid_price from item_list where num=%d", primary_num);
    mysql_query(conn, query);
    sql_result=mysql_store_result(conn);
    while((sql_row=mysql_fetch_row(sql_result))){
        highest_bid = atoi(sql_row[0]);
    }
    mysql_free_result(sql_result);
    return highest_bid;
}


void update_history(MYSQL *conn, user *user1, int primary_num, int bid_cost){
    char query[150];
    sprintf(query, "update bid_history set bid_cost = %d where num=%d and bidder=\'%s\'", bid_cost, primary_num, user1->email);
    mysql_query(conn, query);
}


void new_history(MYSQL *conn, user *user1, int primary_num, int bid_cost){
    char query[150];
    sprintf(query, "insert into bid_history (num, bidder, bid_cost) values(%d, %d, \'%s\')", primary_num, bid_cost, user1->email);
    mysql_query(conn, query);
}


bool check_history(MYSQL *conn, user *user1, int primary_num, int bid_cost){
    MYSQL_RES *sql_result;
    MYSQL_ROW sql_row;
    char query[150];
    int check = 0;

    sprintf(query, "select * from bid_history where num=%d and bidder=\'%s\'", primary_num, user1 -> email);
    mysql_query(conn, query);
    sql_result=mysql_store_result(conn);
    while((sql_row=mysql_fetch_row(sql_result))){
        check = atoi(sql_row[0]);
    }   
    if(check > 0){
        update_history(conn, user1, primary_num, bid_cost);
    }
    else{
        new_history(conn, user1, primary_num, bid_cost);
    }
    mysql_free_result(sql_result);
}


int buy_now_price(MYSQL *conn, int primary_num){
        MYSQL_RES *sql_result;
    MYSQL_ROW sql_row;
    char query[100];
    int price;
    sprintf(query, "select buy_now_price from item_list where num=%d", primary_num);
    mysql_query(conn, query);
    sql_result=mysql_store_result(conn);
    while((sql_row=mysql_fetch_row(sql_result))){
        price = atoi(sql_row[0]);
    }
    mysql_free_result(sql_result);
    return price;
}


void update_bidder(MYSQL *conn, user* user1, int primary_num, int bid_price){
    char query[150];
    check_history(conn, user1, primary_num, bid_price);
    sprintf(query, "update item_list set bid_price=%d, buyer=\'%s\' where num=%d", bid_price, user1 -> email, primary_num);
    mysql_query(conn, query);
}


void update_buyer(MYSQL *conn, user* user1, int primary_num){
    char query[150];
    char sold_date[20];

    time_t t = time(NULL);
    struct tm tm = *localtime(&t);
    sprintf(sold_date, "%d-%d-%d %d:%d", tm.tm_year+1900, tm.tm_mon+1, tm.tm_mday, tm.tm_hour, tm.tm_min);

    sprintf(query, "update item_list set buyer=\'%s\', is_traded=1, sold_date=\'%s\' where num=%d", user1 -> email, sold_date, primary_num);
    mysql_query(conn, query);
}


void trade(MYSQL* conn, user* user1, int* item_primary){
    char select_text[10];
    int select_num;
    int primary_num;
    char bid_text[10];
    int bid_price;
    int high_bid;
    bool escape = true;

    printf("--- Which item do you want to bid? (Enter the number or ’B’ to go back to the previous menu): ");
    scanf("%s", select_text);
    if(strcmp(select_text, "B") == 0){
        return;
    }
    select_num = atoi(select_text);
    primary_num = item_primary[select_num - 1];

    while(escape){
        while (getchar() != '\n');
        printf("--- Bidding price? (Enter the price or ’buy’ to pay for the buy-it-now price) :" );
        scanf("%s", bid_text);
        if(strcmp(select_text, "B") == 0){
            escape = false;
        }
        else if(strcmp(bid_text, "buy") == 0){
            update_buyer(conn, user1, primary_num);
            escape = false;
        }
        else{
            bid_price = atoi(bid_text);
            high_bid = highest_bid(conn, primary_num);
            if(bid_price <= high_bid){
                printf("low than current bid\n");
            }
            else{
                update_bidder(conn, user1, primary_num, bid_price);
                escape = false;
            }
        }
    }
    return;
}