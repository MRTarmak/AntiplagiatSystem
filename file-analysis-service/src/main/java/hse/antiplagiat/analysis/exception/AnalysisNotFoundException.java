package hse.antiplagiat.analysis.exception;

public class AnalysisNotFoundException extends FileAnalysisException {
    public AnalysisNotFoundException(String message) {
        super(message);
    }

    public AnalysisNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
