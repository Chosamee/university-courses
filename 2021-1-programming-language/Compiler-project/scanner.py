import string


class Letter:
    def __init__(self):
        self.bracket = ['(', ')', '{', '}']
        self.statement = ['IF', 'THEN', 'ELSE']
        self.ending = ['EXIT']
        self.operator = ['>', '+', '=']
        self.vtypes = ['int', 'char']
        self.unique = [';']


def tokenizer(code):
    token = []
    cnt = 0
    letter = Letter()
    while cnt < len(code):
        check = code[cnt]
        if check.isdigit():
            k = 0
            while code[cnt+k].isdigit():
                k += 1
            token.append(['Num', code[cnt:cnt+k]])
            cnt = cnt + k
        elif check.isalpha():
            k = 0
            while code[cnt+k].isalpha():
                k += 1
            coin = code[cnt:cnt+k]
            if coin in letter.statement:
                token.append(['Statement', coin])
            elif coin in letter.ending:
                token.append(['EXIT', coin])
            elif coin in letter.vtypes:
                token.append(['Type', coin])
            else:
                token.append(['Word', coin])
            cnt = cnt + k

        elif check.isspace():
            cnt += 1

        else:
            if check in letter.bracket:
                token.append(['Bracket', check])
            elif check in letter.operator:
                token.append(['Operator', check])
            elif check in letter.unique:
                token.append(['Unique', check])
            else:
                print("Error Scanner")
                exit()
            cnt += 1

    return token
