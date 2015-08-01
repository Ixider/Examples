#pragma once

using namespace System;

template <class NT>
ref class QNODE {
public:
	QNODE(NT value) {
	   Value = value;
	}

	NT Value;
	QNODE<NT>^ Next;
};

template <class T>
ref class Queue
{
private:
	QNODE<T>^ head;
	QNODE<T>^ tail;
	void addNode(QNODE<T>^ newQNODE);
	void deleteQNODE(QNODE<T>^ stackTop);
	void reverseRecurse(QNODE<T>^ currennode);


public:
	Queue(void);

	void Push(T n);
	T Pop();
	T Peek();

	int Count();	
	bool IsEmpty();
	void Clear();
	void RReverse();
	void IReverse();
};

template<class T>
Queue<T>::Queue(void) {
	head = nullptr;
	tail = nullptr;
}

/* PRE: */
template<class T>
void Queue<T>::Push(T n) {
	QNODE<T>^ node = gcnew QNODE<T>(n);
	addNode(node);
}
/* POST: */

/* PRE:  isEmpty must be called and return false value */
template<class T>
T Queue<T>::Pop() {
	T n = tail->Value;
	deleteQNODE(tail);
	return n;
}
/* POST: */

/* PRE: */
template<class T>
T Queue<T>::Peek() {
	return tail->Value;
}
/* POST: */

/* PRE: */
template<class T>
int Queue<T>::Count() {
	QNODE<T>^ node = head;

	int count = 0;
	while(node != nullptr) {
		count++;
		node = node->Next;
	}

	return count;
}
/* POST: */

/* PRE: */
template<class T>
bool Queue<T>::IsEmpty() {
	if(tail == nullptr)
		return true;
	return false;
}
/* POST: */

/* PRE: */
template<class T>
void Queue<T>::IReverse() {

	QNODE^ aNode = nullptr;
	QNODE^ bNode = head;
	QNODE^ cNode;

	while(bNode != nullptr) {
		cNode = bNode->Next;
		bNode->Next = aNode;
		aNode = bNode;
		bNode = cNode;
	}

	head = aNode;
}
/* POST: */

/* PRE: */
template<class T>
void Queue<T>::RReverse() {
	reverseRecurse(head);
}

template<class T>
void Queue<T>::reverseRecurse(QNODE<T>^ node) {
	if(node->Next == nullptr) {
		head = node;
		return;
	}

	reverseRecurse(node->Next);
	node->Next->Next = node;
	node->Next = nullptr;
}
/* POST: */

/* PRE: */
template<class T>
void Queue<T>::Clear() {
	QNODE^ node = head;

	while(node != nullptr) {
		QNODE^ tempQNODE = node;
		deleteQNODE(node);
		node = tempQNODE->Next;
	}
}
/* POST: */


/* PRE: */
template<class T>
void Queue<T>::addNode(QNODE<T>^ newQNODE) {
	if(head == nullptr) {
		head = newQNODE;
		tail = newQNODE;
	}
	else {
		tail->Next = newQNODE;
		tail = newQNODE;
	}
}
/* POST: */


/* PRE: */
template<class T>
void Queue<T>::deleteQNODE(QNODE<T>^ dNode) {
	QNODE<T>^ node = head;

	if(dNode == head) {
		if(node->Next == nullptr) {
			head = nullptr;
			tail = nullptr;
		}
		else {
			head = node->Next;
		}
	}
	else
	{
		while(node->Next != dNode) {
			node = node->Next;	
		}
		if(node->Next == tail) {
			tail = node;
		}
		node->Next = dNode->Next;
	}
        
	delete(node->Next);
}
/* POST: */
