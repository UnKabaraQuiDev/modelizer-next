package lu.kbra.modelizer_next.bootstrap;

import java.io.File;
import java.util.Queue;

public interface AppMain {

	void start(String[] args, Queue<File> toBeOpened);

}
