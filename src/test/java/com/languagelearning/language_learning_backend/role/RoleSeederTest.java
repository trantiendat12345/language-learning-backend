package com.languagelearning.language_learning_backend.role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.role.entity.Role;
import com.languagelearning.language_learning_backend.role.repository.RoleRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoleSeederTest {

    @Mock
    private RoleRepository roleRepository;

    @Captor
    private ArgumentCaptor<Role> roleCaptor;

    @Test
    void run_whenRolesDoNotExist_createsAdminAndUserRoles() {
        RoleSeeder seeder = new RoleSeeder(roleRepository);
        when(roleRepository.findByCode("ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.findByCode("USER")).thenReturn(Optional.empty());

        seeder.run();

        verify(roleRepository, times(2)).save(roleCaptor.capture());
        assertThat(roleCaptor.getAllValues())
                .extracting(Role::getCode)
                .containsExactlyInAnyOrder("ADMIN", "USER");
    }

    @Test
    void run_whenRolesAlreadyExist_doesNotCreateDuplicates() {
        RoleSeeder seeder = new RoleSeeder(roleRepository);
        when(roleRepository.findByCode("ADMIN")).thenReturn(Optional.of(new Role()));
        when(roleRepository.findByCode("USER")).thenReturn(Optional.of(new Role()));

        seeder.run();

        verify(roleRepository, never()).save(any());
    }
}
