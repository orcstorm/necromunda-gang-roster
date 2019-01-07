package forms

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc._

import models._

object Forms {
  
  val fighterForm = Form(
    mapping(
        "id" -> number,
        "gangId" -> number,
        "name" -> text,
        "fighterType" -> number)
    (Fighter.apply)(Fighter.unapply)
  )

  val weaponForm = Form(
    mapping(
      "id" -> number,
      "fighterId" -> number,
      "weaponId" -> number)
    (FighterWeapon.apply)(FighterWeapon.unapply)
  )

  val gangForm = Form(
    mapping(
      "id" -> number,
      "house" -> number,
      "name" -> text)
    (Gang.apply)(Gang.unapply)
  )

  val weaponTraitForm = Form(
    mapping(
      "id" -> number,
      "weaponId" -> number,
      "traitId" -> number)
    (WeaponsTraits.apply)(WeaponsTraits.unapply)
  )

  val weaponCostForm = Form(
    mapping(
    "id" -> number,
    "houseId" -> number,
    "weaponId" -> number,
    "credits" -> number)
    (WeaponCost.apply)(WeaponCost.unapply)
  )

  val skillForm = Form(
    mapping(
      "id" -> number,
      "name" -> text,
      "skillType" -> text,
      "description" -> text)
    (Skill.apply)(Skill.unapply)
  )

  val fighterSkillForm = Form(
    mapping(
      "id" -> number,
      "fighterId" -> number,
      "skillId" -> number)
    (FighterSkill.apply)(FighterSkill.unapply)
  )

  val wargearForm = Form(
    mapping(
      "id" -> number,
      "name" -> text,
      "description" -> text)
    (Wargear.apply)(Wargear.unapply)
  )

  val fighterWargearForm = Form(
    mapping(
      "id" -> number,
      "fighterId" -> number, 
      "wargearId" -> number)
    (FighterWargear.apply)(FighterWargear.unapply)
  )

  val wargearCostForm = Form(
    mapping(
    "id" -> number,
    "houseId" -> number,
    "wargearId" -> number,
    "credits" -> number)
    (WargearCost.apply)(WargearCost.unapply)
  )

  val combiForm = Form(
    mapping(
      "id" -> number,
      "name" -> text)
    (Combi.apply)(Combi.unapply)
  )

  val combiWeaponForm = Form(
    mapping(
      "id" -> number,
      "combiId" -> number, 
      "weaponId" -> number)
    (CombiWeapon.apply)(CombiWeapon.unapply)
  )

  val combiCostForm = Form(
    mapping(
    "id" -> number,
    "houseId" -> number,
    "combiId" -> number,
    "credits" -> number)
    (CombiCost.apply)(CombiCost.unapply)
  )

  val combiFighterForm = Form(
    mapping(
      "id" -> number,
      "fighterId" -> number,
      "combiId" -> number)
    (CombiFighter.apply)(CombiFighter.unapply)
  )

  val traitForm = Form(
    mapping(
      "id" -> number,
      "name" -> text,
      "description" -> text)
    (WeaponTrait.apply)(WeaponTrait.unapply)
  )

}