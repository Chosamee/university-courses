import sys
from scanner import *
from LL1_parser import *
from AST import *
from code_generator import *


if __name__ == '__main__':
    with open('testfile.txt', 'r') as testfile:
        code = testfile.read()

    with open('grammar.txt', 'r') as grammarFile:
        grammar_txt = grammarFile.read()

    token = tokenizer(code)

    raw_grammar = grammar_txt_to_dic(grammar_txt)
    grammar = remove_recursion(raw_grammar)

    first = find_first(grammar)
    follow = find_follow(grammar, first)

    terminal, non_terminal = get_terminal(grammar)
    parsing_table = make_parsing_table(grammar, terminal, non_terminal, follow)

    just_token, orig_token = make_just_token(token)
    parsing_tree = parsing(just_token, orig_token, terminal, non_terminal, parsing_table)

    symbol_table = make_symbol_table(parsing_tree)
    print(parsing_tree)

    print('token')
    for i in token:
        print(i)

    print('\n first')
    for i in first:
        print(i)

    print('\n follow')
    for i in follow:
        print(i)

    print('\n parsing table')
    print(non_terminal)
    j = 0
    for i in parsing_table:
        print(terminal[j], i)
        j += 1

    print('\n AST')
    print(parsing_tree)

    gen = Generator(parsing_tree)
    ir = gen.ir()

    print(symbol_table)
    # with open("compiler2021.py.symbol", "w") as symbol_file:
    #     symbol_file.write(symbol_table)

    with open("compiler2021.py.code", "w") as code_file:
        for i, r in enumerate(ir):
            code_file.write(str(i) + ". " + r[0] + '\n')
