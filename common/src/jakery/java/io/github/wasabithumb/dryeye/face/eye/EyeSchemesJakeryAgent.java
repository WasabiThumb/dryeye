package io.github.wasabithumb.dryeye.face.eye;

import io.github.wasabithumb.jakery.agent.Group;
import io.github.wasabithumb.jakery.agent.JakeryAgent;
import io.github.wasabithumb.jakery.agent.set.ClassSet;

import java.lang.reflect.Modifier;

public final class EyeSchemesJakeryAgent extends JakeryAgent {

    @Group(EyeSchemes.JAKERY_GROUP)
    public ClassSet findImpls() {
        return find("io.github.wasabithumb.dryeye.face.eye.impl")
                .withoutModifiers(Modifier.ABSTRACT | Modifier.INTERFACE)
                .withSupertype(EyeScheme.class);
    }

}
