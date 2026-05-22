package io.github.wasabithumb.dryeye.face.eye;

import io.github.wasabithumb.dryeye.face.Face;
import io.github.wasabithumb.jakery.Jakery;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;

/**
 * Registry for standard {@link EyeScheme eye schemes}.
 * @see #all()
 * @see #match(Face)
 */
@NullMarked
public final class EyeSchemes {

    static final String JAKERY_GROUP = "eyeSchemeImpls";
    private static EyeScheme @Nullable [] IMPLS;

    //

    static synchronized EyeScheme[] impls() {
        EyeScheme[] impls = IMPLS;
        if (impls == null) {
            Set<Class<?>> implClasses = Jakery.jakery(EyeSchemes.class.getClassLoader())
                    .typeGroup(JAKERY_GROUP);

            impls = new EyeScheme[implClasses.size()];
            int head = 0;

            for (Class<?> cls : implClasses) {
                EyeScheme impl;
                try {
                    Constructor<?> con = cls.getConstructor();
                    con.setAccessible(true);
                    impl = (EyeScheme) con.newInstance();
                } catch (Exception e) {
                    throw new IllegalStateException("failed to instantiate eye scheme impl class " + cls.getName(), e);
                }
                impls[head++] = impl;
            }

            if (head != impls.length) {
                // ???
                EyeScheme[] shrink = new EyeScheme[head];
                System.arraycopy(impls, 0, shrink, 0, head);
                impls = shrink;
            }

            IMPLS = impls;
        }
        return impls;
    }

    public static @Unmodifiable List<EyeScheme> all() {
        return List.of(impls());
    }

    /**
     * Returns the standard {@link EyeScheme eye scheme}
     * for which the {@link EyeScheme#weight(Face) weight} with respect
     * to the provided skin is highest.
     */
    public static EyeScheme match(Face face) {
        final EyeScheme[] impls = impls();

        EyeScheme ret = impls[0];
        double weight = ret.weight(face);

        for (int i = 1; i < impls.length; i++) {
            EyeScheme candidate = impls[i];
            double candidateWeight = candidate.weight(face);
            if (candidateWeight <= weight) continue;
            ret = candidate;
            weight = candidateWeight;
        }

        return ret;
    }

    //

    private EyeSchemes() { }

}
