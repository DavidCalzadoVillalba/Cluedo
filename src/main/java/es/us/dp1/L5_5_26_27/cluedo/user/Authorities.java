package es.us.dp1.L5_5_26_27.cluedo.user;

import es.us.dp1.L5_5_26_27.cluedo.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "authorities")
public class Authorities extends BaseEntity{

	@Column(length = 20, unique = true, nullable = false)
	String authority;
	
	
}
