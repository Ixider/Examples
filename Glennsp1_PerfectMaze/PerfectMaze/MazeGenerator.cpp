#include "stdafx.h"
#include "MazeGenerator.h"

MazeGenerator::MazeGenerator(Config^ sConfig) {
	rGen = gcnew Random();
	config = sConfig;
}

// MAZE GENERATION
array<Node^,2>^ MazeGenerator::buildMaze() {
	array<Node^, 2>^  mazeGrid = gcnew array<Node^, 2>(config->MAX_ROWS, config->MAX_COLS);
	for (int i = 0; i < config->MAX_COLS; i++) {
		for (int j = 0; j < config->MAX_ROWS; j++) {
			mazeGrid[i,j] = gcnew Node(Point(i, j), true, true, true, true, false);
		}
	}
	createPath(mazeGrid);
	return mazeGrid;
}

void MazeGenerator::createPath(array<Node^, 2>^ maze) {

	Queue<Node^>^ nodeQueue = gcnew Queue<Node^>();
	maze[0,0]->Visited = true;
	nodeQueue->Push(maze[0,0]);

	Node^ currentNode;
	while(!allNodesVisisted(maze)) {
		currentNode = nodeQueue->Peek();

		System::Collections::Generic::List<Node^>^ adjNodes = retrieveUnvistedAdjacentNodes(currentNode, maze);
		
		if(adjNodes->Count > 0) {
			int i = rGen->Next(0,adjNodes->Count);
			Node^ nextNode = adjNodes[i];
			breakWall(currentNode, nextNode);
			nextNode->Visited = true;
			nodeQueue->Push(nextNode);
		}
		else {
			nodeQueue->Pop();
		}
	}

	resetVisitedCells(maze);
}

void MazeGenerator::breakWall(Node^ currentNode, Node^ nextNode) {
	if(nextNode->Address.X > currentNode->Address.X) { //East case
		currentNode->East = false;
		nextNode->West = false;
	}else if(nextNode->Address.X < currentNode->Address.X){ // West case
		currentNode->West = false;
		nextNode->East = false;
	}else if(nextNode->Address.Y > currentNode->Address.Y) { // South case
		currentNode->South = false;
		nextNode->North = false;
	} else if(nextNode->Address.Y < currentNode->Address.Y) { // North case
		currentNode->North = false;
		nextNode->South = false;
	}
}

System::Collections::Generic::List<Node^>^ MazeGenerator::retrieveUnvistedAdjacentNodes(Node^ currentNode, array<Node^, 2>^ maze) {
	System::Collections::Generic::List<Node^>^ adjNodes = gcnew System::Collections::Generic::List<Node^>();

	Point ca = currentNode->Address;
	
	if(ca.X - 1 >= 0) //Add Node to the West
		adjNodes->Add(maze[ca.X -1, ca.Y]);
	if(ca.Y - 1 >= 0) //Add Node to the North
		adjNodes->Add(maze[ca.X, ca.Y - 1]);
	if (ca.X + 1 < config->MAX_COLS) //Add node to the East
		adjNodes->Add(maze[ca.X + 1, ca.Y]);
	if (ca.Y + 1 < config->MAX_ROWS) //Add node to the South
		adjNodes->Add(maze[ca.X, ca.Y + 1]);

	//Filters out already vistited nodes
	for(int i = (adjNodes->Count - 1); i >= 0; i--)
		if(adjNodes[i]->Visited)
			adjNodes->RemoveAt(i);

	return adjNodes;
}


bool MazeGenerator::allNodesVisisted(array<Node^,2>^ maze) {
	int Total = config->MAX_COLS * config->MAX_ROWS;

	int Count = 0;

	for (int i = 0; i < config->MAX_COLS; i++)
		for (int j = 0; j < config->MAX_ROWS; j++)
			if(maze[i,j]->Visited == true)
				Count++;
		
	if(Count == Total)
		return true;

	return false;
}

void MazeGenerator::resetVisitedCells(array<Node^, 2>^ maze) {
	for (int i = 0; i < config->MAX_COLS; i++)
		for (int j = 0; j < config->MAX_ROWS; j++)
			maze[i,j]->Visited = false;
}






