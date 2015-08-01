#pragma once
#include "Node.h"
#include "Config.h"
#include "Queue.h"

using namespace::System;
using namespace::System::Drawing;

ref class Display {
private:
	Graphics^ gfx;
	void drawCell(Point cellAddress, Node^ node);
	Config^ config;

public:
	Display(Graphics^ graphics);
	void DrawMaze(array<Node^,2>^ maze);
	void DrawSolution(Queue<Node^>^ path);
	void ClearScreen();

};