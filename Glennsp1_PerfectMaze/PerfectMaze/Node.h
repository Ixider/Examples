#pragma once
using namespace::System::Drawing;

ref class Node {
public:
	Node(Point a, bool n, bool e, bool s, bool w, bool v) {
		Address = a;

		North = n;
		East = e;
		South = s;
		West = w;

		Visited = v;
	}

	Point Address;
	bool Visited;
	bool North, East, South, West;
};