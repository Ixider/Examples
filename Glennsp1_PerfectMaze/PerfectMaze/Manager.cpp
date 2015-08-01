#include "stdafx.h"
#include "Manager.h"

Manager::Manager(Display ^Display) {
	display = Display;
	init(gcnew Config());
}

void Manager::init(Config^ config) {
	mg = gcnew MazeGenerator(config);
	ms = gcnew MazeSolver(config);
	clearCanvas();
}

void Manager::createMaze() {
	clearCanvas();
	maze = mg->buildMaze();
	display->DrawMaze(maze);
}

void Manager::draw() {
	Queue<Node^>^ path = ms->solveMaze(maze);
	display->DrawSolution(path);
}

void Manager::clearCanvas() {
	display->ClearScreen();
}