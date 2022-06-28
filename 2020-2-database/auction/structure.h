#ifndef STRUCTURE
#define STRUCTURE

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct _user{
    char email[50];
    char password[50];
}user;

int how_many_bidders(MYSQL *conn, int primary_num);

#endif