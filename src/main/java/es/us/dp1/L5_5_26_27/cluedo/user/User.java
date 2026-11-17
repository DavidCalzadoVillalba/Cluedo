package es.us.dp1.L5_5_26_27.cluedo.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import es.us.dp1.L5_5_26_27.cluedo.model.BaseEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "appusers")
public class User extends BaseEntity {

	@NotBlank
	@Size(max = 255)
	@Column(unique = true)
	String username;

	// Accepted in request bodies but never included in responses, so the hash is never exposed to clients.
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	String password;

	@NotBlank
	@Email
	@Size(max = 100)
	@Column(unique = true, nullable = false, length = 100)
	String email;

	@Size(max = 255)
	@Column(length = 255)
	String avatar;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "account_status", nullable = false, length = 20)
	AccountStatus accountStatus = AccountStatus.ACTIVE;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "authority")
	Authorities authority;

	public Boolean hasAuthority(String auth) {
		return authority.getAuthority().equals(auth);
	}

	public Boolean hasAnyAuthority(String... authorities) {
		Boolean cond = false;
		for (String auth : authorities) {
			if (auth.equals(authority.getAuthority()))
				cond = true;
		}
		return cond;
	}

}
