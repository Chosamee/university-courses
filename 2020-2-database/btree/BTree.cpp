// Please implement most of your source codes here. 

#include "BTree.h"
#include <stdio.h>

BTreeNode::BTreeNode()
{
    ;
}

BTreeNode::~BTreeNode()
{
    ;
}

NodeType
BTreeNode::getNodeType()
{
    return this->type;
}

BTreeInternalNode::BTreeInternalNode() : BTreeNode()
{
    this->type = NodeType::INTERNAL;
    for (int i = 0; i < NUM_KEYS; i++)
    {
        // key must be >0 according to B+tree simulator
        this->keys[i] = 0;
    }
    for (int i = 0; i < NUM_KEYS + 1; i++)
    {
        this->child[i] = nullptr;
    }
}

BTreeInternalNode::~BTreeInternalNode()
{
    for (int i = 0; i < NUM_KEYS + 1; i++)
    {
        delete this->child[i];
    }
}

bool
BTreeInternalNode::isFull()
{
    // check last index
    return (this->child[NUM_KEYS] != nullptr);
}

bool
BTreeInternalNode::insert(long long value)
{
    int i = 0;
    for (auto key : keys)
    {
        if ((key == 0) || (key > value))
        {
            break;
        }
        i++;
    }

    // something went wrong while adding to the children node...
    if (!this->child[i]->insert(value))
    {
        // cannot add new node because the node is full
        if (this->isFull())
        {
            return false;
        }
        else
        {
            for (int j = NUM_KEYS - 1; j > i; j--)
            {
                this->child[j + 1] = this->child[j];
                this->keys[j] = this->keys[j - 1];
            }

            BTreeNode **split_children = this->child[i]->split();
            this->child[i] = split_children[0];
            this->child[i + 1] = split_children[1];
            delete[] split_children;
            split_children = nullptr;
            if (this->child[i]->getNodeType() == NodeType::LEAF)
            {
                ((BTreeLeafNode*)this->child[i + 1])->setRightSibling((BTreeLeafNode*)this->child[i + 2]);
                this->keys[i] = ((BTreeLeafNode *)this->child[i + 1])->getKeyAt(0);
            }
            else
            {
                this->keys[i] = ((BTreeInternalNode *)this->child[i + 1])->keys[0];
            }
            
            // retry insertion (should work)
            this->insert(value);
        }
    }

    return true;
}

BTreeNode*
BTreeInternalNode::getChildWith(long long value)
{
    for (int i = 0; i < NUM_KEYS; i++)
    {
        if (this->keys[i] > value || this->keys[i] == 0)
        {
            return this->child[i];
        }
    }
    return this->child[NUM_KEYS];
}

BTreeNode**
BTreeInternalNode::split()
{
    BTreeInternalNode *child_left = new BTreeInternalNode;
    BTreeInternalNode *child_right = new BTreeInternalNode;
    int left_len = NUM_KEYS / 2 + NUM_KEYS % 2;

    for (int j = 0; j < left_len; j++)
    {
        child_left->keys[j] = this->keys[j];
        child_left->child[j] = this->child[j];
    }
    child_left->child[left_len] = this->child[left_len];

    for (int j = left_len; j < NUM_KEYS; j++)
    {
        child_right->keys[j - left_len] = this->keys[j];
        child_right->child[j - left_len + 1] = this->child[j + 1];
    }

    BTreeNode **ret = new BTreeNode *[2];
    ret[0] = child_left;
    ret[1] = child_right;

    return ret;
}

// hope the connection is made in order...
void
BTreeInternalNode::connect(BTreeNode *node)
{
    for (int i = 0; i < NUM_KEYS + 1; i++)
    {
        if (this->child[i] == nullptr)
        {
            this->child[i] = node;
            if (i > 0)
            {
                if (node->getNodeType() == NodeType::INTERNAL)
                {
                    this->keys[i-1] = ((BTreeInternalNode *)node)->keys[0];
                }
                else
                {
                    this->keys[i-1] = ((BTreeLeafNode *)node)->getKeyAt(0);
                }
            }
            return;
        }
    }
}

BTreeLeafNode::BTreeLeafNode() : BTreeNode()
{
    this->type = NodeType::LEAF;
    for (int i = 0; i < NUM_KEYS; i++)
    {
        // key must be >0 according to B+tree simulator
        this->keys[i] = 0;
    }
    this->right_sibling = nullptr;
}

BTreeLeafNode::~BTreeLeafNode()
{
    // right_sibling will be freed by its parent
    this->right_sibling = nullptr;
}

void
BTreeLeafNode::printLeafNode()
{
    for (int i = 0; i < NUM_KEYS; i++)
    {
        if (this->keys[i] == 0)
        {
            break;
        }

        printf("%lld", this->keys[i]);
        if ((i < NUM_KEYS - 1) && this->keys[i+1] > 0)
        {
            printf(", ");
        }
    }
    printf("\n");
}

bool
BTreeLeafNode::isFull()
{
    // check last index
    return (this->keys[NUM_KEYS - 1] > 0);
}

bool
BTreeLeafNode::insert(long long value)
{
    // cannot insert if node is full
    if (this->isFull())
        return false;

    for (int i = 0; i < NUM_KEYS; i++)
    {
        if (this->keys[i] == 0)
        {
            this->keys[i] = value;
            break;
        }

        if (this->keys[i] > value)
        {
            // printf("val: %lld, key: %lld", value, keys[i]);
            for (int j = NUM_KEYS - 1; j > i; j--)
            {
                this->keys[j] = this->keys[j - 1];
            }
            this->keys[i] = value;
            break;
        }
    }

    return true;
}

BTreeNode*
BTreeLeafNode::getChildWith(long long value)
{
    for (int i = 0; i < NUM_KEYS; i++)
    {
        if (this->keys[i] == value)
        {
            return this;
        }
        else if (this->keys[i] == 0)
        {
            return nullptr;
        }
    }

    // key does not exist in this node
    return nullptr;
}

void
BTreeLeafNode::printKeysBetween(long long low, long long high)
{
    for (int i = 0; i < NUM_KEYS; i++)
    {
        if ((this->keys[i] >= low) && (this->keys[i] < high))
        {
            printf("%lld", this->keys[i]);
            if (i < (NUM_KEYS-1) && (this->keys[i+1] >= low) && (this->keys[i+1] < high))
            {
                printf(", ");
            }
        }
    }
}

BTreeLeafNode*
BTreeLeafNode::getRightSibling()
{
    return right_sibling;
}

void
BTreeLeafNode::setRightSibling(BTreeLeafNode* sibling)
{
    this->right_sibling = sibling;
}

long long
BTreeLeafNode::getKeyAt(int index)
{
    return this->keys[index];
}

BTreeNode**
BTreeLeafNode::split()
{
    BTreeLeafNode *child_left = this;
    BTreeLeafNode *child_right = new BTreeLeafNode;

    int left_len = NUM_KEYS / 2 + NUM_KEYS % 2;
    child_left->right_sibling = child_right;
    for (int j = left_len; j < NUM_KEYS; j++)
    {
        child_right->keys[j-left_len] = this->keys[j];
        child_left->keys[j] = 0;
    }

    BTreeNode **ret = new BTreeNode*[2];
    ret[0] = child_left;
    ret[1] = child_right;

    return ret;
}

BTree::BTree()
{
    root = new BTreeLeafNode;
}

BTree::~BTree()
{
    delete root;
    root = nullptr;
}

BTreeLeafNode*
BTree::findLeafNode(long long value)
{
    BTreeNode *parent = this->root;
    BTreeNode *child = nullptr;
    while (true)
    {
        child = parent->getChildWith(value);
        if (child == nullptr)
            // this is when the key does not exist in this tree
            return nullptr;
        else if (child == parent)
            // this is assured to be leaf node with the key
            // so force typecast and return
            return (BTreeLeafNode*)child;

        parent = child;
    }
}

void
BTree::insert(long long value)
{
    // if insertion failed for some reason...
    if (!root->insert(value))
    {
        BTreeNode **split_children = this->root->split();
        BTreeInternalNode *new_root = new BTreeInternalNode;
        new_root->connect(split_children[0]);
        new_root->connect(split_children[1]);
        delete[] split_children;
        this->root = new_root;
        this->root->insert(value);
    }
}

void
BTree::printLeafNode(long long value)
{
    BTreeLeafNode *leaf_node = this->findLeafNode(value);
    if (leaf_node == nullptr)
    {
        printf("NOT FOUND\n");
    }
    else
    {
        leaf_node->printLeafNode();
    }
}

void
BTree::pointQuery(long long value)
{
    BTreeLeafNode *leaf_node = this->findLeafNode(value);
    if (leaf_node == nullptr)
    {
        printf("NOT FOUND\n");
    }
    else
    {
        printf("%lld\n", value);
    }
}

void
BTree::rangeQuery(long long low, long long high)
{
    BTreeNode *parent = this->root;
    BTreeNode *child = nullptr;
    BTreeLeafNode *low_cur = nullptr;
    BTreeLeafNode *high_cur = nullptr;
    BTreeLeafNode *leaf_cur = nullptr;

    // look for leaf with low value
    while (true)
    {
        child = parent->getChildWith(low);
        if ((child == nullptr) || (child == parent))
        {
            low_cur = (BTreeLeafNode*)parent;
            break;
        }

        parent = child;
    }

    parent = this->root;
    // look for leaf with high value
    while (true)
    {
        child = parent->getChildWith(high);
        if ((child == nullptr) || (child == parent))
        {
            high_cur = (BTreeLeafNode*)parent;
            break;
        }

        parent = child;
    }

    leaf_cur = low_cur;
    while (true)
    {
        leaf_cur->printKeysBetween(low, high);
        // proceed to right_sibling
        if (leaf_cur != high_cur)
        {
            printf(", ");
            leaf_cur = leaf_cur->getRightSibling();
        }
        // do not proceed to right_sibling
        else
        {
            printf("\n");
            break;
        }

        // the last leaf was the rightmost leaf node
        if (leaf_cur == nullptr)
        {
            printf("\n");
            break;
        }
    }
}
