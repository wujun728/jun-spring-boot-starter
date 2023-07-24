package com.gitthub.wujun728.engine.bytecodes.execute;

import java.util.ArrayList;

public class HackArrayList  extends ArrayList {
	public HackArrayList(int initialCapacity) {
		super(5);
	}
	public HackArrayList(){
		this(5);
	}
}
