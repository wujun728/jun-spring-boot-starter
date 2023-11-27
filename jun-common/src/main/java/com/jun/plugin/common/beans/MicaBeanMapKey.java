
package com.jun.plugin.common.beans;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 * bean map key，提高性能
 *
 * @author L.cm
 */
@EqualsAndHashCode
@RequiredArgsConstructor
public class MicaBeanMapKey {
	private final Class type;
	private final int require;
}
