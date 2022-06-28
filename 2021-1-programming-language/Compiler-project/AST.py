class Node:
    def __init__(self, data, parent, sibling_order, val=None):
        self.data = data
        self.parent = parent
        self.sibling_order = sibling_order
        self.children = []
        self.val = val
        self.sethi = 1

    def __repr__(self, level = 0):
        value = self.data
        ret = str(level) + "|" + "\t\t" * level + repr(value)
        if len(self.children) == 0:
            ret += " *LEAF\n"
        else:
            ret += "\n"
        for child in self.children:
            ret += child.__repr__(level + 1)
        return ret

    def make_children(self, data):
        for i, dat in enumerate(data):
            self.children.append(Node(dat, self, i))

    def get_next(self):
        sibling_order = self.sibling_order
        node = self
        parent = self.parent
        while True:
            if parent is not None:
                if len(parent.children)-1 == sibling_order:
                    sibling_order = parent.sibling_order
                    node = parent
                    parent = parent.parent
                else:
                    break
            else:
                return node
        node = parent.children[sibling_order+1]
        while len(node.children) != 0:
            node = node.children[0]
        return node

    def get_root(self):
        node = self
        while node.parent is not None:
            node = node.parent
        return node


def make_symbol_table(node):
    scope = ['global']
    symbol_table = []
    while len(node.children) != 0:
        node = node.children[0]

    symbol_table.append([node.val, "function", scope])
    # input func name
    scope.append(node.val)

    node = node.get_next()
    while node.parent is not None:
        if node.data in ["int", "char"]:
            symbol = node.data
            size = 1
            if symbol == "int":
                size = 4
            while node.data != ';':
                node = node.get_next()
                if node.data == "([a-z] | [A-Z])*":
                    symbol_table.append([node.val, symbol, scope, "size = {}".format(size)])
        elif node.data == "IF":
            scope.append(node.data)
        elif node.data == "ELSE":
            scope.append("ELSE")
        elif node.data == "}":
            scope.pop()
        node = node.get_next()

    return symbol_table


def make_just_token(token):
    just_token = []
    orig_token = []
    for types, tok in token:
        if types == "Word":
            just_token.append("([a-z]|[A-Z])*")
        elif types == "Num":
            just_token.append("[0-9]*")
        else:
            just_token.append(tok)
        orig_token.append(tok)
    return just_token, orig_token


def parsing(just_token, orig_token, terminal, non_terminal, parsing_table):
    terminal_idx = {}
    non_terminal_idx = {}
    for idx, key in enumerate(terminal):
        terminal_idx[key] = idx
    for idx, key in enumerate(non_terminal):
        non_terminal_idx[key] = idx

    just_token.append('$')
    # use stack
    stack = [terminal[0], '$']
    node = Node(stack[0], None, 0)
    while len(stack) > 1:
        top = stack[0]
        stack = stack[1:]
        tok = just_token[0]
        orig_top = orig_token[0]
        if top in non_terminal_idx:
            if tok == top:
                just_token = just_token[1:]
                orig_token = orig_token[1:]
                if tok == "([a-z]|[A-Z])*":
                    node.val = orig_top
                elif tok == "[0-9]*":
                    node.val = orig_top
                node = node.get_next()
            else:
                print("Error Parsing Code1")
                exit()
        else:
            push = parsing_table[terminal_idx[top]][non_terminal_idx[tok]]
            if push is not None:
                if push != ['']:
                    stack = push + stack
                    node.make_children(push)
                    node = node.children[0]
                else:
                    node.make_children([''])
                    node = node.get_next()
            else:
                print("Error Parsing Code2")
                exit()
    return node.get_root()
