package io.github.gms.secure.controller;

import io.github.gms.common.abstraction.AbstractController;
import io.github.gms.common.enums.EventOperation;
import io.github.gms.common.enums.EventTarget;
import io.github.gms.common.types.AuditTarget;
import io.github.gms.common.types.Audited;
import io.github.gms.secure.dto.ChangePasswordRequestDto;
import io.github.gms.secure.dto.PagingDto;
import io.github.gms.secure.dto.SaveEntityResponseDto;
import io.github.gms.secure.dto.SaveUserRequestDto;
import io.github.gms.secure.dto.UserDto;
import io.github.gms.secure.dto.UserListDto;
import io.github.gms.secure.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static io.github.gms.common.util.Constants.ALL_ROLE;
import static io.github.gms.common.util.Constants.ROLE_ADMIN;
import static io.github.gms.common.util.Constants.ROLE_ADMIN_OR_USER;

/**
 * @author Peter Szrnka
 * @since 1.0
 */
@RestController
@RequestMapping("/secure/user")
@AuditTarget(EventTarget.USER)
public class UserController extends AbstractController<UserService> {

	public UserController(UserService service) {
		super(service);
	}
	
	@PostMapping
	@PreAuthorize(ROLE_ADMIN_OR_USER)
	@Audited(operation = EventOperation.SAVE)
	public SaveEntityResponseDto save(@RequestBody SaveUserRequestDto dto) {
		return service.save(dto);
	}

	@GetMapping("/{id}")
	@PreAuthorize(ROLE_ADMIN_OR_USER)
	public UserDto getById(@PathVariable("id") Long id) {
		return service.getById(id);
	}
	
	@PostMapping("/list")
	@PreAuthorize(ROLE_ADMIN)
	public UserListDto list(@RequestBody PagingDto dto) {
		return service.list(dto);
	}
	
	@DeleteMapping("/{id}")
	@PreAuthorize(ROLE_ADMIN)
	@Audited(operation = EventOperation.DELETE)
	public ResponseEntity<String> delete(@PathVariable("id") Long id) {
		service.delete(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping("/{id}")
	@PreAuthorize(ROLE_ADMIN)
	@Audited(operation = EventOperation.TOGGLE_STATUS)
	public ResponseEntity<String> toggle(@PathVariable("id") Long id, @RequestParam boolean enabled) {
		service.toggleStatus(id, enabled);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@PostMapping("/change_credential")
	@PreAuthorize(ALL_ROLE)
	public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequestDto dto) {
		service.changePassword(dto);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping(value = "/mfa_qr_code", produces = MimeTypeUtils.IMAGE_PNG_VALUE)
	@PreAuthorize(ALL_ROLE)
	public ResponseEntity<byte[]> getMfaQrCode() {
		try {
			return new ResponseEntity<>(service.getMfaQrCode(), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/toggle_mfa")
	@PreAuthorize(ALL_ROLE)
	@Audited(operation = EventOperation.TOGGLE_MFA)
	public ResponseEntity<Void> toggleMfa(@RequestParam boolean enabled) {
		service.toggleMfa(enabled);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/mfa_active")
	@PreAuthorize(ALL_ROLE)
	public ResponseEntity<Boolean> isMfaActive() {
		return new ResponseEntity<>(service.isMfaActive(), HttpStatus.OK);
	}
}
