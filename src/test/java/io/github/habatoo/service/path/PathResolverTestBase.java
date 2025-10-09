package io.github.habatoo.service.path;

import io.github.habatoo.service.PathResolver;
import io.github.habatoo.service.impl.PathResolverImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class PathResolverTestBase {

    protected static final String BASE_UPLOAD_DIR = "uploads/posts/";

    protected PathResolver pathResolver;

    @BeforeEach
    void setUp() {
        pathResolver = new PathResolverImpl(BASE_UPLOAD_DIR);
    }
}
