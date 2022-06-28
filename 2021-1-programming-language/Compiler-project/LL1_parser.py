def grammar_txt_to_dic(grammar_txt):
    grammar = {}
    grammar_txt = grammar_txt.replace("\n", " ")

    grammar_state = grammar_txt.split("; ")
    grammar_list = []

    for i in grammar_state:
        grammar_list.append(i.replace("\"", ""))

    for state in grammar_list:
        value = []
        left, right = state.split('::=')
        left = left.replace(' ', '')
        if left != 'word':
            or_case = right.split("|")
        else:
            grammar[left] = [[right.replace(' ', '')]]
            continue
        for gram in or_case:
            splited = gram.split(" ")

            for i in splited:
                if i == '':
                    splited.remove(i)

            value.append(splited)

        grammar[left] = value
    return grammar


def remove_recursion(raw_grammar):
    grammar = {}
    for key in raw_grammar.keys():
        idx = [0, 0]
        check = [False, False]
        for i, value in enumerate(raw_grammar[key]):
            if key == value[0]:
                idx[0] = i
                check[0] = True
            elif len(value) == 1:
                idx[1] = i
                check[1] = True
        if check[0] and check[1]:
            if raw_grammar[key][idx[1]][0] == '':
                grammar[key] = [[key+'_1']]
            else:
                grammar[key] = [[raw_grammar[key][idx[1]][0], key+'_1']]
            grammar[key+'_1'] = [raw_grammar[key][idx[0]][1:] + [key+'_1'], ['']]
        else:
            grammar[key] = raw_grammar[key]
    return grammar


def find_first_sub(key, grammar):
    first_list = []
    if key not in grammar.keys():
        return [key]
    else:
        for values in grammar[key]:
            first = find_first_sub(values[0], grammar)
            if type(first) is list:
                first_list = first_list + first
            else:
                first_list.append(first)
        return first_list


def find_first(grammar):
    first_dic = {}
    for key in grammar.keys():

        first_dic[key] = set()
        for values in grammar[key]:
            first = find_first_sub(values[0], grammar)

            if first != '':
                first_dic[key] = first_dic[key].union(set(first))
            else:
                first_dic[key].add('')
    return first_dic


def find_follow_sub(m_key, grammar, first):
    key_list = list(grammar.keys())
    follow = set()
    for key in key_list:
        for value in grammar[key]:
            for i in range(len(value)):
                if value[i] == m_key:
                    if i + 1 < len(value):
                        next_f = set()
                        if value[i + 1] in key_list:
                            next_f = first[value[i + 1]]
                        else:
                            next_f.add(value[i + 1])

                        if '' in next_f and m_key != key:
                            fol = find_follow_sub(key, grammar, first)
                            follow = follow.union(fol)
                        follow = follow.union(next_f-{''})
                    else:
                        if m_key != key:
                            fol = find_follow_sub(key, grammar, first)
                            if len(fol) == 0:
                                fol.add('$')
                            follow = follow.union(fol)
    return follow


def find_follow(grammar, first):
    follow_dic = {}
    key_list = list(grammar.keys())
    for m_key in key_list:
        follow = find_follow_sub(m_key, grammar, first)
        if len(follow) == 0:
            follow.add('$')
        follow_dic[m_key] = follow
    return follow_dic


def get_terminal(grammar):
    '''
    :param grammar:
    :return: list, list
    '''
    terminal = list(grammar.keys())
    non_terminal = set()
    for key in grammar.keys():
        for value in grammar[key]:
            for i in value:
                if i not in terminal and i != '':
                    non_terminal = non_terminal.union({i})
    return terminal, sorted(list(non_terminal.union({'$'})))


def make_parsing_table(grammar, terminal, non_terminal, follow):
    parsing_table = []
    for i in range(len(terminal)):
        initial = []
        for j in range(len(non_terminal)):
            initial.append(None)
        parsing_table.append(initial)
    for key in grammar.keys():
        idx1 = terminal.index(key)
        fol = follow[key]
        for value in grammar[key]:
            first = find_first_sub(value[0], grammar)
            for first_val in first:
                if first_val != '':
                    parsing_table[idx1][non_terminal.index(first_val)] = value
            if '' in first:
                for f in fol:
                    parsing_table[idx1][non_terminal.index(f)] = value
    return parsing_table
