import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {RelationInfo, SoftwareInfo, SoftwareService, TeamMemberInfo} from '../generated/software';
import { SoftwareConstantsInfo } from '../generated/software/model/softwareConstantsInfo';
import { IdAndDisplaynameInfo } from '../generated/software/model/idAndDisplaynameInfo';



@Component({
  selector: 'app-softwaredetails',
  templateUrl: './softwaredetails.component.html',
  styleUrls: ['./softwaredetails.component.scss']
})
export class SoftwareDetailsComponent implements OnInit {

  @Input()
  software: SoftwareInfo;

  constants: SoftwareConstantsInfo = {};

  relations: RelationInfo[];

  allsoftware: SoftwareInfo[];

  constructor(private softwareService: SoftwareService, private router:Router, private route: ActivatedRoute) {
   }

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.softwareService.getSoftwareById(params.id).subscribe(
        x => this.software = x,
        err => console.error('Call to get software by ID recieved an error: ' + err),
        () => console.info('Call to get software by ID finished (' + params.id + ") -> " + this.software?.id)
      );

      this.softwareService.getOtherSoftwareList(params.id).subscribe(
        x => this.allsoftware = x,
        err => console.error('Call to get other software recieved an error: ' + err),
        () => console.info('Call to get other software finished (' + params.id + ") -> " + this.allsoftware?.length)
      );

      this.softwareService.getRelationsBySoftware(params.id).subscribe(
        x => this.relations = x,
        err => console.error('Call to get relations of software recieved an error: ' + err),
        () => console.info('Call to get relations of software finished (' + params.id + ") -> " + this.relations?.length)
      );

    });


    this.softwareService.getConstants().subscribe(
      x => this.constants = x,
      err => console.error('Call to get software constants got an error: ' + err),
      () => console.info('Call to get software constants finished'),
    )

  }

  save () {
    console.log("save called for " + this.software.id)
    this.softwareService.updateSoftware(this.software.id, this.software).subscribe(
              x => {},
        err => console.error('Call to update software ' + this.software.id + ' by ID got an error: ' + JSON.stringify(err) + "-" + this.software?.id + "-"),
        () => console.info('Call to update software by ID finished)'),
      );

    this.softwareService.updateRelations(this.relations).subscribe(
      x => {},
      err => console.error('Call to update relations ' + this.software.id + ' by ID got an error: ' + JSON.stringify(err) + "-" + this.software?.id + "-"),
      () => console.info('Call to update relations by ID finished)'),

    )
  }

  stepToOverview () {
    console.log("stepToOverview called")
    this.router.navigate(['/app/software']);

  }

  addTechnology () {
    console.log("addTechnology called")


    var newTechObject: IdAndDisplaynameInfo = {};

    this.software.technologies = [...this.software.technologies, newTechObject ];
    console.log("Added : " + this.software.technologies)
  }

  removeTechnology (technology: IdAndDisplaynameInfo) {
    console.log("removetechnology called with selected technology " + JSON.stringify(technology));
    this.software.technologies = this.software.technologies.filter(obj => obj !== technology)
  }

  removeRelation (relation: RelationInfo) {
    console.log ("removeRelation called with selected relation " + JSON.stringify(relation))
    this.relations = this.relations.filter(obj => obj !== relation)
  }

  removeContact (contact: TeamMemberInfo) {
    console.log ("removeContact called with selected contact " + JSON.stringify(contact))
    this.software.teammembers = this.software.teammembers.filter(obj => obj !== contact)
  }

  addContact () {
    console.log ("addContact called")
    var newTeamMember: TeamMemberInfo = {}
    this.software.teammembers = [...this.software.teammembers, newTeamMember];
  }

  addRelation () {
    console.log ("addRelation called")
    var newRelation: RelationInfo = {source: this.software, target: null}
    this.relations = [...this.relations, newRelation];
  }

}
