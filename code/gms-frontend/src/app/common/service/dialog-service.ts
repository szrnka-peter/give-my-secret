import { Injectable } from "@angular/core";
import { MatDialog, MatDialogRef } from "@angular/material/dialog";
import { ConfirmDeleteDialog } from "../components/confirm-delete/confirm-delete-dialog.component";
import { InfoDialog } from "../components/info-dialog/info-dialog.component";

/**
 * @author Peter Szrnka
 */
@Injectable({ providedIn: 'root' })
export class DialogService {

    constructor(private dialog: MatDialog) { }

    openInfoDialog(title: string, text: string): MatDialogRef<InfoDialog, any> {
        return this.dialog.open(InfoDialog, {
            data: { title: title, text: text, type: 'information' }
        });
    }

    openWarningDialog(text: string): MatDialogRef<InfoDialog, any> {
        return this.dialog.open(InfoDialog, {
            data: { text: text, type: 'warning' }
        });
    }

    openConfirmDeleteDialog(confirmMessage: string): MatDialogRef<ConfirmDeleteDialog, any> {
        return this.dialog.open(ConfirmDeleteDialog, {
            data: {
                result: true,
                confirmMessage: confirmMessage
            }
        });
    }
}