#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <mysql/mysql.h>
#include "main_menu.c"
#include "structure.h"

// typedef struct _user{
//     char email[50];
//     char password[50];
// } user;


user* login(MYSQL *conn){
    user* data = (user*)malloc(sizeof(user));    
    
    printf("----< Login >\n"
            "---- email: ");
    scanf("%s", data->email);
    getchar();
    
    printf("---- password: ");
    scanf("%s", data->password);

    return data;
}


bool check_account(MYSQL *conn, user* check_user){
    MYSQL_RES *sql_result;
    MYSQL_ROW sql_row;
    int query_stat;
    bool is_exist;

    char query[150];
    sprintf(query, "select * from user where email=\'%s\' and password=\'%s\'", check_user -> email, check_user -> password);

    mysql_query(conn, query);
    sql_result = mysql_store_result(conn);

    if(!mysql_fetch_row(sql_result)){
        printf("wrong account");
        is_exist = false;
    }
    else is_exist = true;

    mysql_free_result(sql_result);
    return is_exist;
}


bool check_admin(MYSQL *conn, user* check_user){
    MYSQL_RES *sql_result;
    MYSQL_ROW sql_row;
    int query_stat;
    bool is_exist;

    char query[150];
    sprintf(query, "select * from admin where email=\'%s\' and password=\'%s\'", check_user -> email, check_user -> password);

    mysql_query(conn, query);
    sql_result = mysql_store_result(conn);

    if(!mysql_fetch_row(sql_result)){
        printf("wrong account");
        is_exist = false;
    }
    else is_exist = true;

    mysql_free_result(sql_result);
    return is_exist;
}


void sign_up(MYSQL *conn){
    char first[20], last[10], mail[50], passwd[50];
    printf("----< Sign Up >\n"
            "---- first name: ");
    scanf("%s", first);
    getchar();
    
    printf("---- last name: ");
    scanf("%s", last);
    getchar();
    
    printf("---- email: ");
    scanf("%s", mail);
    getchar();

    printf("---- password: ");
    scanf("%s", passwd);

    char query[150];
    sprintf(query, "insert into user values(\'%s\', \'%s\')", mail, passwd);
    mysql_query(conn, query);
}


void login_menu(MYSQL *conn){
    int num;
    bool check = false;
    user* now_user;

    while(true){

        printf("----< Login menu >\n"
        "----(1) Login. // After login, show < Main menu >\n"
        "----(2) Sign up // After sign up, show < Login menu >\n"
        "----(3) Login as Administrator\n"
        "----(4) Quit // Exit the program\n");
        printf("Your Choice: ");
        scanf("%d", &num);

    switch(num){
        case 1:
            now_user = login(conn);
            check = check_account(conn, now_user);
            if(check){
                printf("\nWelcome %s\n\n", now_user -> email);
                main_menu(conn, now_user);
            }
            break;
        case 2:
            sign_up(conn);
            break;
        case 3:
            now_user = login(conn);
            check = check_admin(conn, now_user);
            break;
            
        case 4:
            exit(1);
        default :
            exit(1);
        }
    }
}

