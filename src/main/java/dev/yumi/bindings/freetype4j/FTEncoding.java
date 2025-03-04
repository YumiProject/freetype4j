/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.bindings.freetype4j;

import java.util.List;

public enum FTEncoding {
	MS_SYMBOL("symb"),
	UNICODE("unic"),
	SJIS("sjis"),
	PRC("gb  "),
	BIG5("big5"),
	WANSUNG("wans"),
	JOHAB("joha"),
	ADOBE_STANDARD("ADOB"),
	ADOBE_EXPERT("ADBE"),
	ADOBE_CUSTOM("ADBC"),
	ADOBE_LATIN_1("lat1"),
	OLD_LATIN_2("lat2"),
	APPLE_ROMAN("armn");

	public static final List<FTEncoding> VALUES = List.of(values());
	private final int id;

	FTEncoding(String id) {
		this.id = (id.codePointAt(0) << 24)
				| (id.codePointAt(1) << 16)
				| (id.codePointAt(2) << 8)
				| id.codePointAt(3);
	}

	public int id() {
		return this.id;
	}

	public static FTEncoding byId(int id) {
		return VALUES.stream().filter(ft -> ft.id() == id).findFirst().orElse(null);
	}
}
