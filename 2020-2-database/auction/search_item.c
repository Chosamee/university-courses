#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <mysql/mysql.h>
#include "structure.h"
#include "trade.c"


int count_row(MYSQL *conn){
    MYSQL_RES *sql_result;
    MYSQL_ROW sql_row;
    int row_num;
    char *query = "select count(*) from item_list";
    mysql_query(conn, query);
    sql_result=mysql_store_result(conn);
    while((sql_row=mysql_fetch_row(sql_result))){
        row_num = atoi(sql_row[0]);
    }
    return row_num;
}


int* print_search(MYSQL *conn, user *user1, char *query, int item_num){
    MYSQL_RES *sql_result;
    MYSQL_ROW sql_row;
    int query_stat;
    int item_cnt = 1;
    int *item_primary = (int*) malloc((sizeof(int)) * item_num);

    mysql_query(conn, query);
    sql_result=mysql_store_result(conn);

    int field=mysql_num_fields(sql_result);

    while((sql_row=mysql_fetch_row(sql_result))){
        printf("[Item %d]\n", item_cnt);
        printf("description: %s\n", sql_row[1]);
        printf("status: %s bids\n", sql_row[7]);
        printf("current bidding price: %s\n", sql_row[3]);
        printf("current highest bidder: %s\n", sql_row[4]);
        printf("date posted: %s\n", sql_row[5]);
        printf("bid ending date: %s\n\n", sql_row[6]);
        item_primary[item_cnt - 1] = atoi(sql_row[0]);
        item_cnt++;
    }

    mysql_free_result(sql_result);
    return item_primary;
}


void search_by_category(MYSQL *conn, user *user1, int item_num){
    int cat_num;
    int *item_id;
    int *item_primary;

    char* category[] = {"", "Electronics", "Books", "Home", "Clothing", "Sporting Goods"};

    printf("----< Search items by category > : (Enter the number)\n"
            "----(1) Electronics\n"
            "----(2) Books\n"
            "----(3) Home\n"
            "----(4) Clothing\n"
            "----(5) Sporting Goods");
    printf("Your Choice: ");
    scanf("%d", &cat_num);

    char query[200];
    sprintf(query, "select num, description, is_traded, bid_price, buyer, posted_date,\
            bid_end_date, bidder_num, sold_date, buy_now_price from item_list where is_traded=0 and category=\'%s\'", category[cat_num]);
    item_primary = print_search(conn, user1, query, item_num);
    trade(conn, user1, item_primary);

}


void search_by_description(MYSQL *conn, user *user1, int item_num){
    char des_keyword[200];
    int *item_primary;
    printf("----< Search items by description keyword >"
            "---- Search keyword : ");
    scanf("%s", des_keyword);

    char query[200];
    sprintf(query, "select num, description, is_traded, bid_price, buyer, posted_date,\
            bid_end_date, bidder_num, sold_date, buy_now_price from item_list where is_traded=0 and description like \'%%%s%%\'", des_keyword);
    item_primary = print_search(conn, user1, query, item_num);
    trade(conn, user1, item_primary);
}


void search_by_seller(MYSQL *conn, user *user1, int item_num){
    char seller[50];
    int *item_primary;
    printf("----< Search items by seller >"
            "---- Search seller : ");
    scanf("%s", seller);

    char query[200];
    sprintf(query, "select num, description, is_traded, bid_price, buyer, posted_date,\
            bid_end_date, bidder_num, sold_date, buy_now_price from item_list where is_traded=0 and seller=\'%s\'", seller);
    item_primary = print_search(conn, user1, query, item_num);
    trade(conn, user1, item_primary);
}


void search_by_date(MYSQL *conn, user *user1, int item_num){
    char date[20];
    int *item_primary;
    printf("----< Search items by date posted >"
            "---- Search date posted (yyyy-mm-dd) : ");
    scanf("%s", date);

    char query[200];
    sprintf(query, "select num, description, is_traded, bid_price, buyer, posted_date,\
            bid_end_date, bidder_num, sold_date, buy_now_price from item_list where is_traded=0 and date=\'%%%s%%\'", date);
    item_primary = print_search(conn, user1, query, item_num);
    trade(conn, user1, item_primary);
}


void search_item(MYSQL *conn, user *user1){
    int num;
    bool escape_search = true;
    int item_num = count_row(conn);

    while(escape_search){
        while (getchar() != '\n');
        printf("----< Search item > : (Enter the number)\n"
                "----(1) Search items by category\n"
                "----(2) Search items by description keyword\n"
                "----(3) Search items by seller\n"
                "----(4) Search items by date posted\n"
                "----(5) Go Back\n"
                "----(6) Quit\n");
        printf("Your Choice: ");
        scanf("%d", &num);

        switch(num){
            case 1:
                search_by_category(conn, user1, item_num);
                break;
            case 2:
                search_by_description(conn, user1, item_num);
                break;
            case 3:
                search_by_seller(conn, user1, item_num);
                break;
            case 4:
                search_by_date(conn, user1, item_num);
                break;
            case 5:
                escape_search = false;
                break;
            case 6:
                exit(1);
            default :
                exit(1);

        }
    }
}   