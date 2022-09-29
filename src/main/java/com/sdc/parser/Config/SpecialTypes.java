package com.sdc.parser.Config;

public final class SpecialTypes {
	private SpecialTypes() {
		// restrict instantiation
	}

	public static enum ResponseType {
		INTEGER("integer"),
		DECIMAL("decimal"),
		STRING("string"),
		BOOLEAN("boolean"),
		DATE("date"),
		DATETIME("datetime"),
		TIME("time");
		private String value;
		private ResponseType(String value) {
			this.value = value;
		}
		public static ResponseType strToResponseType(String string) {
			ResponseType type = null;
			for (ResponseType rType : ResponseType.values()) {
				if (rType.value.equals(string)){
					type = rType;
				}
			}
			// if (type == null) {
				// try {
					// throw new Exception("Type: " + string + " not expected");
				// } catch (Exception e) {
				// 	e.printStackTrace();
				// }
			// }
			return type;
		}
	}

	public static enum ObservationType {
		LIST,
		MULTISELECT,
		TEXT
	}

}
