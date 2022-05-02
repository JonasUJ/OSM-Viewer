package dialog;

import java.io.File;

public class CreateMapDialogResult implements DialogResult {
    public final File file;

    CreateMapDialogResult(File file) {
        this.file = file;
    }
}
