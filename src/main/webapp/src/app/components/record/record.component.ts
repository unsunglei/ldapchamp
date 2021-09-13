import { Component, OnInit } from '@angular/core';
import {ApiService} from "../../services/api.service";

@Component({
  selector: 'app-record',
  templateUrl: './record.component.html',
  styleUrls: ['./record.component.scss']
})
export class RecordComponent implements OnInit {

  debug = false;
  searchFilter = '';
  searchBase = '';

  record$: any;
  searchResult$: any;

  dnKey = 'dn';

  constructor(private api: ApiService) { }

  ngOnInit(): void {
    this.config();
  }

  public config() {
    this.api.config().subscribe(
      (next: any) => {
        this.dnKey = next.dnKey;
      },
      error => {
        console.error(error);
      }
    )
  }

  public search() {
    // this.record$ = this.api.getUser(this.searchInput);
    this.searchResult$ = this.api.search(this.searchBase, this.searchFilter);
  }

  public searchExact(entryDN: string) {
    this.record$ = this.api.getUser(entryDN);
  }

}
