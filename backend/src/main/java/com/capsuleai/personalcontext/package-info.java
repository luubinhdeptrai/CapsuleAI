/**
 * Personal Context business module.
 *
 * <p>Owns the user's declared preferences and needs, selected location,
 * and the meaning of current or limited contextual information, including
 * weather evidence used by other capabilities.
 *
 * <p>Other modules use published {@code api} contracts rather than importing
 * private profile models, application services, or persistence classes.
 * Context ownership and withdrawal rules remain within this module.
 *
 * <p>Architecture reference:
 * {@code docs/04-architecture/views/implementation-view.puml}.
 */
package com.capsuleai.personalcontext;
