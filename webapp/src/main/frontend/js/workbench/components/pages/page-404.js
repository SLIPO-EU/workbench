import * as React from 'react';
import { Container, Row, Col, Button } from 'reactstrap';
import { NavLink } from 'react-router-dom';

export default class Page404 extends React.Component {
  render() {
    return (
      <div className='app flex-row align-items-center'>
        <Container>
          <Row className='justify-content-center'>
            <Col md='6'>
              <div className='clearfix'>
                <h1 className='float-left display-3 mr-4'>404</h1>
                <h4 className='pt-3'>Oops! You're lost.</h4>
                <p className='text-muted float-left'>The page you are looking for was not found.</p>
              </div>
              <NavLink to='/dashboard'>
                <Button color='primary'><i className='fa fa-home'></i>&nbsp; Take Me Home</Button>
              </NavLink>
            </Col>
          </Row>
        </Container>
      </div>
    );
  }
}
