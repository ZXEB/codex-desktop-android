package com.ai.codex.shizukuexec;

import java.util.List;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public final class PythonRuntimeTest {
    private static final String DATA = "lib/python3.14/os.py";
    private static final String ALIAS = "lib/python3.14/lib-dynload/_struct.so";
    private static final String OLD_LINK = ALIAS + ".aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    private static final String EXTRA_FILE = "lib/python3.14/__pycache__/os.cpython-314.pyc";

    @Test public void reportsOnlyUnlistedEntriesForRecoveryAndAcceptsTheCleanTree() throws Exception {
        Set<String> signed = Set.of(DATA, ALIAS);
        AdmissionTrace trace = new AdmissionTrace();
        trace.at(AdmissionTrace.Stage.PYTHON_TREE);
        Deployment.UnexpectedPythonFiles error = assertThrows(Deployment.UnexpectedPythonFiles.class,
            () -> PythonRuntime.requireExactTree(signed, Set.of(DATA, ALIAS, OLD_LINK, EXTRA_FILE), trace));

        assertEquals(List.of(EXTRA_FILE, OLD_LINK), error.paths());
        assertEquals("unexpected:" + EXTRA_FILE, trace.result(false, error).getString("subject"));
        PythonRuntime.requireExactTree(signed, signed, new AdmissionTrace());
    }

    @Test public void missingRequiredEntryDoesNotOfferQuarantine() throws Exception {
        Set<String> signed = Set.of(DATA, ALIAS);
        AdmissionTrace trace = new AdmissionTrace();
        trace.at(AdmissionTrace.Stage.PYTHON_TREE);
        SecurityException error = assertThrows(SecurityException.class,
            () -> PythonRuntime.requireExactTree(signed, Set.of(DATA, OLD_LINK), trace));

        assertFalse(error instanceof Deployment.UnexpectedPythonFiles);
        assertEquals("missing:" + ALIAS, trace.result(false, error).getString("subject"));
    }
}
