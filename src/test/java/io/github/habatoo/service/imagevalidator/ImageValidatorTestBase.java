package io.github.habatoo.service.imagevalidator;

import io.github.habatoo.service.ImageValidator;
import io.github.habatoo.service.impl.ImageValidatorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
abstract class ImageValidatorTestBase {

    protected ImageValidator imageValidator;

    @BeforeEach
    void setUp() {
        imageValidator = new ImageValidatorImpl();
    }
}
