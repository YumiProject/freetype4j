/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.bindings.freetype4j;

public class FreeTypeException extends RuntimeException {
	private final int code;

	public FreeTypeException(int code, String message) {
		super(message);
		this.code = code;
	}

	public FreeTypeException(int code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	public FreeTypeException(int code, Throwable cause) {
		super(cause);
		this.code = code;
	}

	/**
	 * {@return the associated FreeType error code}
	 */
	public int code() {
		return this.code;
	}
}
