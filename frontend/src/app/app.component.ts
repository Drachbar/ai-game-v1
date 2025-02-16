import {Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {HttpClient} from '@angular/common/http';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'frontend';

  constructor(private httpClient: HttpClient) {
  }

  getUsers() {
    this.httpClient.get<any>('http://localhost:8080/api/users').subscribe(users => {
      console.log(users);
    })
  }

  addUser() {
    this.httpClient.post('http://localhost:8080/api/users', {name: 'Mattias', email: 'mattias@example.com'})
      .subscribe(user => {
        console.log(user);
      })
  }
}
