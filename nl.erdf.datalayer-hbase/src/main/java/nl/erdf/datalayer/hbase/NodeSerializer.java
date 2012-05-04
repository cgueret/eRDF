package nl.erdf.datalayer.hbase;

import java.io.IOException;

import org.apache.hadoop.io.DataInputBuffer;
import org.apache.hadoop.io.DataOutputBuffer;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

public class NodeSerializer {
	protected static final byte URI = 0;
	protected static final byte PLAIN_LITERAL = 1;
	protected static final byte TYPED_LITERAL = 2;
	protected static final byte LANG_LITERAL = 3;
	protected static final byte BNODE = 4;
	protected static final ValueFactory f = new ValueFactoryImpl();

	/**
	 * @param bytes
	 * @return
	 */
	public static Value fromBytes(byte[] in) {
		Value resource = null;
		DataInputBuffer dib = new DataInputBuffer();
		dib.reset(in, in.length);
		try {
			byte type = dib.readByte();
			String label = dib.readUTF();
			switch (type) {
			case BNODE:
				resource = f.createBNode(label);
				break;
			case PLAIN_LITERAL:
				resource = f.createLiteral(label);
				break;
			case TYPED_LITERAL:
				URI datatype = f.createURI(dib.readUTF());
				resource = f.createLiteral(label, datatype);
				break;
			case LANG_LITERAL:
				String lang = dib.readUTF();
				resource = f.createLiteral(label, lang);
				break;
			case URI:
				return f.createURI(label);
			default:
				throw new IllegalArgumentException("Unrecognized object type: " + type);
			}

		} catch (IOException e) {
			throw new IllegalStateException("Something went wrong in deserialization");
		}

		return resource;
	}

	/**
	 * @param resource
	 * @return
	 */
	public static byte[] toBytes(Value resource) {
		DataOutputBuffer dob = new DataOutputBuffer(2048);
		dob.reset();

		try {
			if (resource instanceof BNode) {
				dob.write(BNODE);
				dob.writeUTF(((BNode) resource).getID());
			} else if (resource instanceof URI) {
				dob.write(URI);
				dob.writeUTF(((URI) resource).stringValue());
			} else if (resource instanceof Literal) {
				if (((Literal) resource).getDatatype() == null && ((Literal) resource).getLanguage() == null) {
					dob.write(PLAIN_LITERAL);
					dob.writeUTF(((Literal) resource).getLabel());
				}
				if (((Literal) resource).getDatatype() != null && ((Literal) resource).getLanguage() == null) {
					dob.write(TYPED_LITERAL);
					dob.writeUTF(((Literal) resource).getLabel());
					dob.writeUTF(((Literal) resource).getDatatype().stringValue());
				}
				if (((Literal) resource).getDatatype() == null && ((Literal) resource).getLanguage() != null) {
					dob.write(LANG_LITERAL);
					dob.writeUTF(((Literal) resource).getLabel());
					dob.writeUTF(((Literal) resource).getLanguage());
				}
			} else {
				throw new IllegalStateException("Unknown resource class" + resource.getClass().getName());
			}
		} catch (IOException e) {
			throw new IllegalStateException("Something went wrong in deserialization");
		}

		byte[] ret = new byte[dob.getLength()];
		System.arraycopy(dob.getData(), 0, ret, 0, dob.getLength());
		return ret;
	}
}
