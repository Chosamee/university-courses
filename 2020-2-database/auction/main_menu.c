#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <mysql/mysql.h>
#include "sell_item.c"
#include "status_my_item.c"
#include "search_item.c"
#include "structure.h"
#include "check_status_bid.c"

void main_menu(MYSQL* conn, user* user1){
    int num;
    bool escape = true;

    while(escape){
        while (getchar() != '\n');
        printf("----< Main menu > : (Enter the number)\n"
                "----(1) Sell item\n"
                "----(2) Status of Your Item Listed on Auction\n"
                "----(3) Search item\n"
                "----(4) Check Status of your Bid\n"
                "----(5) Check your Account\n"
                "----(6) Quit\n");
        printf("Your Choice: ");
        scanf("%d", &num);

        switch(num){
            case 1:
                sell_item(conn, user1);
                break;
            case 2:
                status_my_item(conn, user1);
                break;
            case 3:
                search_item(conn, user1);
                break;
            case 4:
                check_status_bid(conn, user1);
                break;
            case 5:
                // check_my_account(conn, user1);
                break;
            case 6:
                exit(1);
            default :
                exit(1);
            }
    }
}