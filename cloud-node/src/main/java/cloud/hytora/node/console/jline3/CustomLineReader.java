package cloud.hytora.node.console.jline3;

import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;

class CustomLineReader extends LineReaderImpl {

    CustomLineReader(Terminal terminal, String appName) {
        super(terminal, appName, null);
    }

    @Override
    protected boolean historySearchBackward() {
        if (history.previous()) {
            setBuffer(history.current());
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean historySearchForward() {
        if (history.next()) {
            setBuffer(history.current());
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean upLineOrSearch() {
        return historySearchBackward();
    }

    @Override
    protected boolean downLineOrSearch() {
        return historySearchForward();
    }
}
