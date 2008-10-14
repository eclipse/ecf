package org.eclipse.ecf.sync;



public interface IModelSynchronizationStrategy {

	/**
	 * Register local model change with synchronization strategy.  This method
	 * should be synchronously called when a local model change has
	 * been made to the underlying model.
	 * @param localChange the IModelChange made to the local model
	 * @return IModelChangeMessage[] an array of change message to be 
	 * delivered to remote participants.
	 */
	public IModelChangeMessage[] registerLocalChange(IModelChange localChange);

	/**
	 * Transform remote change into a set of local changes to
	 * be synchronously applied to the local model. 
	 * @param remoteChange the remote model change instance to
	 * be transformed by this synchronization strategy.
	 * @return IDocumentChange[] to apply to local model
	 */
	public IModelChange[] transformRemoteChange(IModelChange remoteChange);


	/**
	 * Deserialization of given byte array to concrete instance of
	 * IModelChange object to represent local change to be applied
	 * 
	 * @param bytes the bytes to be deserialized
	 * @return IModelChange instance from bytes.  Will not be <code>null</code>.
	 * @throws SerializationException thrown if some problem deserializing given bytes.
	 */
	public IModelChange deserializeRemoteChange(byte[] bytes) throws SerializationException;

}
