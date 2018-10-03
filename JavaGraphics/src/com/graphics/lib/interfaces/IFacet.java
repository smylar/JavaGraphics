package com.graphics.lib.interfaces;

import com.graphics.lib.Facet;

/**
 * Generate a facet from vertex numbers
 * @author paul.brandon
 *
 */
@FunctionalInterface
public interface IFacet {
    Facet generate(int a, int b, int c);
}
