WarbotProject
=============

A faire : 

	Tactique : 

		FSM :
		-> rester en defense jusqu'à :
			-> avoir au moins n (à déterminer) tourelles
			-> avoir éliminé 5 ennemis
		-> verifier que la base n'est pas attaquée

		-> attaque de la base ennemie : 
			-> strategie d'attaque en groupe de rocket launcher (si on a le temps ?)

		
		Appel d'offre :
		-> bloquer les points clés (point de nourriture) 
			-> soit par un tank ou bien soit par une tourelle (à voir)


	Espion tourne autour de la base enemie
	
	Implementer Ingénieurs, tourelles 

Deja fait (OK) ou possiblement à faire (???) :
	
	tactique des rockets lauchers en def : 
		-> eviter les missiles ennemies (OK)
		-> attaque de l'ennemie le plus faible (OK)
		-> mouvement circulaire defensif (OK)
	
	tactique base :
		-> attribution role explorer (OK)
		-> prevenir ennemie dans la base (OK)
		-> soigner tout ce qui se trouve dans son permettre (utile pour résister à plusieurs vagues) (???)
		-> enregistrer coordonnées base ennemie des que l'espion l'a trouvé (???)
	
	tactique explorer :
		-> cueilleur (OK et ???)
			-> recruteur d'ingénieur ou de rockets launchers pour bloquer nourriture à l'ennemie (???)
		-> espion (OK mais à verifier des qu'il meurt si il est remplacé)

	-> technique de production des unités (attention à l'énergie de la base) :
			-> 10 rockets launchers puis 2 ingé (voir combien de tourelles un ingé peut créer) puis au moins 1 kamikaze
			-> Si moins de 5 rockets launchers vivants, alors on en crée des nouveaux en priorité
	
	tactique Kamikazes (OK)
		
