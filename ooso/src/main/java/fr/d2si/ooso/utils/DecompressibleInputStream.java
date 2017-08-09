package fr.d2si.ooso.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

public class DecompressibleInputStream extends ObjectInputStream {
    public DecompressibleInputStream(InputStream in) throws IOException {
        super(in);
    }

    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        ObjectStreamClass resultClassDescriptor = super.readClassDescriptor();
        Class localClass;
        try {
            localClass = Class.forName(resultClassDescriptor.getName());
        } catch (ClassNotFoundException e) {
            return resultClassDescriptor;
        }
        ObjectStreamClass localClassDescriptor = ObjectStreamClass.lookup(localClass);
        if (localClassDescriptor != null) {
            final long localSUID = localClassDescriptor.getSerialVersionUID();
            final long streamSUID = resultClassDescriptor.getSerialVersionUID();
            if (streamSUID != localSUID) {
                resultClassDescriptor = localClassDescriptor;
            }
        }
        return resultClassDescriptor;
    }
}