package org.eclipse.ecf.example.collab.editor.message;

/**
 * A message sent from a joining peer, needing the current editor model.
 * This message should be caught by the creator, and respond with a EditorChangeMessage.
 * @author kg11212
 *
 */
public class EditorUpdateRequest extends AbstractMessage {
	private static final long serialVersionUID = 7307387852689460016L;

}
