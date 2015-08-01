#include "stdafx.h"
#include "MazeSolver.h"


MazeSolver::MazeSolver(Config^ sConfig) {
	rGen = gcnew Random();
	config = sConfig;
}

Queue<Node^>^ MazeSolver::solveMaze(array<Node^, 2>^ maze) {
	Queue<Node^>^ path = gcnew Queue<Node^>();
	Point endNodeAddress = Point(config->MAX_COLS - 1, config->MAX_ROWS - 1);

	path->Push(maze[0,0]);
	Node^ currentNode;

	while(path->Peek()->Address != endNodeAddress) {
		currentNode = path->Peek();
		System::Collections::Generic::List<Node^>^ possibleMoves = retrievePossibleMoves(currentNode, maze);
		
		if(possibleMoves->Count > 0) {
			int i = rGen->Next(possibleMoves->Count);
			Node^ nextNode = possibleMoves[i];		
			nextNode->Visited = true;
			path->Push(nextNode);
		} else {
			path->Pop(); 
		}
	}

	return path;
}

System::Collections::Generic::List<Node^>^ MazeSolver::retrievePossibleMoves(Node^ currentNode, array<Node^, 2>^ maze) {
	System::Collections::Generic::List<Node^>^ possibleMoves = gcnew System::Collections::Generic::List<Node^>();

	Point ca = currentNode->Address;

	if(!currentNode->North)
		possibleMoves->Add(maze[ca.X, ca.Y - 1]);
	if(!currentNode->East)
		possibleMoves->Add(maze[ca.X + 1, ca.Y]);
	if(!currentNode->South)
		possibleMoves->Add(maze[ca.X, ca.Y + 1]);
	if(!currentNode->West)
		possibleMoves->Add(maze[ca.X - 1, ca.Y]);


	for(int i = possibleMoves->Count - 1; i >= 0; i--)
		if(possibleMoves[i]->Visited)
			possibleMoves->RemoveAt(i);

	return possibleMoves;
}