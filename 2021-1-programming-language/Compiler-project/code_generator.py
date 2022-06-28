def move_inorder(node):
    if node.children:
        return node.children[0]
    while node.parent is not None:
        if node.sibling_order < len(node.parent.children) - 1:
            return node.parent.children[node.sibling_order + 1]
        node = node.parent
    return None


class Generator:
    def __init__(self, node):
        self.node = node

    def ir(self):
        f_name = self.node.children[0].children[0].val
        ir = [["BEGIN " + f_name]]
        ir += self.make_ir(self.node)
        ir += [["END " + f_name]]
        return ir

    def make_ir(self, new):
        self.node = new
        ir = []
        end = new
        while end.children:
            end = end.children[-1]

        while self.node != end:
            if self.node.data == "stat":
                n = self.node.children[0]
                if n.data in ["word", "EXIT"]:
                    ir.append([self.word_ret(n), self.node])
                if n.data == "IF":
                    ir += self.if_ir()
            self.node = move_inorder(self.node)
        return ir

    def word_ret(self, node):
        ir = node.children[0].val if node.children else node.data
        while node.data not in [";", "THEN", "{"]:
            node = node.get_next()
            if node.val:
                ir += " " + node.val
            elif node.data:
                ir += " " + node.data
        return ir[:-2]

    def if_ir(self):
        ir = []
        # IF cond THEN block ELSE block
        cond = self.node.children[1]
        then_s = self.node.children[3]
        else_s = self.node.children[5]

        then_ir = self.make_ir(then_s)
        else_ir = self.make_ir(else_s)

        cond_txt = ""
        while cond.children:
            cond = cond.children[0]
        while cond.data != "{":
            if cond.val:
                cond_txt += " " + cond.val
            elif cond.data:
                cond_txt += " " + cond.data
            cond = cond.get_next()

        ir.append(["if" + cond_txt + " goto L1", cond])
        ir.append(["goto L2"])
        ir.append(["L1"])
        ir += then_ir
        ir.append(["L2"])
        ir += else_ir
        return ir


def generate(node, ir):
    return


def sethi_ullman(node):
    sethi_cnt = 1
    if node.children:
        return


def print_code(node, ir):
    return
