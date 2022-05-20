package com.freya02.botcommands;

import com.freya02.botcommands.test.commands2.MyCommand;

import java.util.List;

public class KtTest {
	public void lol(int i) {
		new MyCommand().test(context -> {

		});
	}

	public List<KtTest> l() { throw new IllegalStateException(); }
}
